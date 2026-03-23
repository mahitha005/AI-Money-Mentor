import pickle
import sys

model = pickle.load(open("finance_model.pkl","rb"))

savings_rate = float(sys.argv[1])
expense_ratio = float(sys.argv[2])
debt_ratio = float(sys.argv[3])
investment_ratio = float(sys.argv[4])

prediction = model.predict([[savings_rate,expense_ratio,debt_ratio,investment_ratio]])

print(prediction[0])